import java.lang.reflect.Method;
public class TestMethods {
    public static void main(String[] args) throws Exception {
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.multiplayer.ClientLevel");
            for (Method m : clazz.getMethods()) {
                if (m.getName().toLowerCase().contains("time") || m.getName().toLowerCase().contains("day")) {
                    System.out.println("ClientLevel Method: " + m.getName() + " return: " + m.getReturnType().getName());
                }
            }
        } catch (Throwable t) { t.printStackTrace(); }
    }
}
