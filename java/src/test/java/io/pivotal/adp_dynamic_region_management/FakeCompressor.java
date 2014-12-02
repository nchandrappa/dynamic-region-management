package io.pivotal.adp_dynamic_region_management;

import com.gemstone.gemfire.compression.Compressor;

public class FakeCompressor implements Compressor {

    @Override
    public byte[] compress(byte[] bytes) {
        return new byte[0];
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        return new byte[0];
    }
}