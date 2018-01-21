package horse.wtf.flaplid;

import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.*;

public class CLIArgumentsTest {

    @Test
    public void testGetConfigFilePath() throws Exception {
        CLIArguments args = new CLIArguments();
        args.setConfigFilePath("/etc/foo.conf");

        assertEquals(args.getConfigFilePath(), "/etc/foo.conf");
    }

    @Test
    public void testHasTags() throws Exception {
        CLIArguments args = new CLIArguments();

        args.setTags("");
        assertFalse(args.hasTags());

        args.setTags(null);
        assertFalse(args.hasTags());

        args.setTags("foo");
        assertTrue(args.hasTags());

        args.setTags("foo,bar");
        assertTrue(args.hasTags());

        args.setTags("foo, bar");
        assertTrue(args.hasTags());

        args.setTags(" foo, bar");
        assertTrue(args.hasTags());

        args.setTags("foo, bar ");
        assertTrue(args.hasTags());

        args.setTags(" foo, bar ");
        assertTrue(args.hasTags());
    }

    @Test
    public void testGetTags() throws Exception {
        CLIArguments args = new CLIArguments();

        args.setTags("");
        assertEquals(args.getTags().size(), 0);

        args.setTags(null);
        assertEquals(args.getTags().size(), 0);

        args.setTags("foo");
        assertEquals(args.getTags(), new ArrayList<String>(){{ add("foo"); }});

        args.setTags("foo,bar");
        assertEquals(args.getTags(), new ArrayList<String>(){{ add("foo"); add("bar"); }});

        args.setTags("foo, bar");
        assertEquals(args.getTags(), new ArrayList<String>(){{ add("foo"); add("bar"); }});

        args.setTags(" foo, bar");
        assertEquals(args.getTags(), new ArrayList<String>(){{ add("foo"); add("bar"); }});

        args.setTags("foo, bar ");
        assertEquals(args.getTags(), new ArrayList<String>(){{ add("foo"); add("bar"); }});

        args.setTags(" foo, bar ");
        assertEquals(args.getTags(), new ArrayList<String>(){{ add("foo"); add("bar"); }});
    }
}