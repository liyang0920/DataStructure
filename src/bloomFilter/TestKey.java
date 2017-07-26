package bloomFilter;

public class TestKey implements BloomKey {
    private String element;

    public TestKey(String ele) {
        element = ele;
    }

    @Override
    public byte[] getBytes() {
        return element.getBytes();
    }
}
