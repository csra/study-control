package de.citec.csra.studycontrol;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RecordLightTest {


    // Test makes only sense while connected with the csra system.
    /*@Test*/
    public void init() {
        Assert.assertTrue(RecordLight.init());
    }
}