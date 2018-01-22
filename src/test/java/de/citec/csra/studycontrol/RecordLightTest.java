package de.citec.csra.studycontrol;

import org.junit.Assert;

import static org.junit.jupiter.api.Assertions.*;

class RecordLightTest {

    @org.junit.jupiter.api.Test
    void init() throws Exception {
          Assert.assertTrue(RecordLight.init());
    }
}