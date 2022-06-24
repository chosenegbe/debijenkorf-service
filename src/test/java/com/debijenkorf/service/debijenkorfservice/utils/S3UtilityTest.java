package com.debijenkorf.service.debijenkorfservice.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class S3UtilityTest {

    @Test
    @DisplayName("Validate filename with / character formatted corrected")
    void s3KeyNameTest() {
        S3Utility s3Utility = new S3Utility();

        String keyName = s3Utility.s3KeyName("hel/word.png");
        Assertions.assertEquals(keyName, "hel_/hel_word.png");

        keyName = s3Utility.s3KeyName("a.png");
        Assertions.assertEquals(keyName, "a.png");

        keyName = s3Utility.s3KeyName("abracadabra.png");
        Assertions.assertEquals(keyName, "abra/cada/abracadabra.png");

    }

}