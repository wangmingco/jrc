package co.wangming.jrc.jrcwebserver;

import org.apache.commons.codec.digest.DigestUtils;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

public class CommonsCodecTest {

    public static void main(String[] args) {
        digest();
    }

    public static String digest() {
        byte[] digest = new DigestUtils(SHA_224).digest("test");
        return "ok";
    }
}
