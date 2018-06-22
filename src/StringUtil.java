public class StringUtil {
    private static final char[] lowerChars =
            {'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static String getRandomName() {
        StringBuffer name = new StringBuffer();
        //给name添加五个随机字母
        for (int i = 0; i < 5; i++) {
            name.append(lowerChars[(int) (Math.random() * 26)]);
        }
        return name.toString();
    }
}
