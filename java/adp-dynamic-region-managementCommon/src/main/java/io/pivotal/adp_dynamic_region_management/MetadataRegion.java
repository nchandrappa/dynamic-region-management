package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.pdx.PdxInstance;

public class MetadataRegion {
	private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz".toLowerCase()
									    + "abcdefghijklmnopqrstuvwxyz".toUpperCase();
	private static final String LETTERS_AND_NUMBERS = LETTERS + "0123456789";
	
    public static final String REGION_ATTRIBUTES_METADATA_REGION = "__regionAttributesMetadata";

    public static final int    REGION_COUNT_WARNING_LEVEL = 1000;
    
    private static Cache cache = null;

    /* Subregions are not yet supported.
     * 
     * Space and full stop are allowed, means OQL needs escape with single quote.
     */
    private static final char[] RESERVED_CHARS = { Region.SEPARATOR_CHAR };
    
    public static String getName(){
    	return REGION_ATTRIBUTES_METADATA_REGION;
    }
    
    public static Region<String,PdxInstance> getMetadataRegion() {
    	if(cache==null) {
            cache = CacheFactory.getAnyInstance();
    	}

        Region<String, PdxInstance> metaRegion = cache.getRegion(REGION_ATTRIBUTES_METADATA_REGION);

        return metaRegion;
    }

    /* Admin overhead of region stats, etc, may outweigh benefits as more and more
     * regions are added.
     */
    public static void checkLimits() {
    	try {
    		Region<?,?> metadataRegion = getMetadataRegion();
    		int size = metadataRegion.size();
    		if(size >= REGION_COUNT_WARNING_LEVEL && size%50==0) {
    			LogWriter logWriter = cache.getLogger();
    			logWriter.warning(metadataRegion.getFullPath() + " has " + size + " entries, possible performance overhead");
    		}
    	} catch (Exception e) {
    		;
    	}
    }

    public static void validateRegionName(final Object key) throws Exception {

    	if(key==null || !(key instanceof String) || ((String)key).length()==0) {
    		throw new Exception("Region name must be non-empty String");
    	}
    	
    	String regionName = (String)key;

    	for(char c : RESERVED_CHARS) {
    		if(regionName.indexOf(c)>=0) {
    			throw new Exception("Region name '" + regionName + "' cannot include reserved char '" + c + "'");
    		}
    	}
    	
		if(regionName.startsWith("__")) {
			throw new Exception("Region name '" + regionName + "' cannot begin '__', reserved for Gemfire");
		}

		// Issue at most warning on region name
		String regionNameClean = cleanRegionName(regionName);
		if(!regionName.equals(regionNameClean)) {
			cache.getLogger().warning("Region name '" + regionName + "' contains special characters");
		} else {
			if(LETTERS.indexOf(regionNameClean.charAt(0))==-1) {
				cache.getLogger().warning("Region name '" + regionName + "' should begin with a letter");
			} else {
				if(regionName.length()>256) {
					cache.getLogger().warning("Region name '" + regionName + "' long, at " + regionName.length() + " chars");
				} else {
					for(String anotherRegionName : getMetadataRegion().keySet()) {
						String anotherRegionNameClean = cleanRegionName(anotherRegionName);
						
						/* Warning if similar but not identical to another region, ignoring
						 * case and special chars.
						 * Identical may be valid, eg. if updating existing region and validating key.
						 */
						if(anotherRegionNameClean.equalsIgnoreCase(regionNameClean) &&
								(!anotherRegionName.equals(regionName))) {
							cache.getLogger().warning("Region name '" + regionName + "' similar to existing region '" + anotherRegionName + "'");
						}
					}
				}
				
			}
		}
    }

    /** <P>Return a version of the region name with special characters stripped,
     * only letters and numbers from the original.
     * </P>
     * 
     * @param regionName
     * @return	- A String, not null but possibly an empty String.
     */
	public static String cleanRegionName(/*@NonNull*/ final String regionName) {
		StringBuffer sb = new StringBuffer();
		
		for(int i=0 ; i<regionName.length() ; i++) {
			char c = regionName.charAt(i);
			
			if(LETTERS_AND_NUMBERS.indexOf(c)!=-1) {
				sb.append(c);
			}
		}
		
		return sb.toString();
	}

	public static void validateRegionOptions(/*NonNull*/ final String regionName, final Object regionOptions) throws Exception {
	    if(regionOptions==null) {
	    	throw new Exception("Region name '" + regionName + "', value cannot be null");
	    }
	    if(!(regionOptions instanceof PdxInstance)) {
	    	throw new Exception("Region name '" + regionName + "', value should be PdxInstance not " + regionOptions.getClass().getCanonicalName());
	    } else {
	    	/*TODO 'server' option is mandatory, but unclear if 'client' should be too.
	    	 * It is possible to sensibly default the client option, to a PROXY region
	    	 * using the pool if exactly one exists.
	    	 */
	    	for(String optionName : new String[] { "server" }) {
		        Object option = ((PdxInstance)regionOptions).getField(optionName);
		        if(option==null) {
		        	throw new Exception("Region name '" + regionName + "', value must specify '" + optionName + "' option");
		        }
	    	}
	    }
	    
	    
	}

}
