import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TestClass {

    @Test
    public void testArgs() {

        String[] args = new String[]{"edit", "reason:", "are", "a", "person"};
        String concat = String.join(" ", Arrays.copyOfRange(args, 1,args.length));

        String reason = concat.split(":")[1];
        System.out.println(reason);
        Assertions.assertTrue(true);
    }

    @Test
    public void testNewLineLore() {

        String reason = "Number: \n 47 Is a \n good \n number";
        for (String s : reason.split("\n")) {
            System.out.println(s);
        }
    }

}