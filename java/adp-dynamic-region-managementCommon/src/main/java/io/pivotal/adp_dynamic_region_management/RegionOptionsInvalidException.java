package io.pivotal.adp_dynamic_region_management;

public class RegionOptionsInvalidException extends Exception {
    public RegionOptionsInvalidException(String message) {
        super(message);
    }

    public RegionOptionsInvalidException(String message, Exception cause) {
        super(message, cause);
    }
}
