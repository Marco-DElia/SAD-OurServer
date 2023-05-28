package ClientProject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class TestLola {
@Test

public void testAdd() {
Lola cut = new Lola();
int result = cut.add(2, 3);
assertEquals(5, result);
}
}
;