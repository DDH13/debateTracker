package com.dineth.debateTracker;

import com.dineth.debateTracker.utils.CustomExceptions;
import com.dineth.debateTracker.utils.StringUtil;
import jdk.jfr.Name;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class StringUtilsTest {
    @Nested
    public class StringUtilTest {

        @Test
        @Name("Split name with regular two parts")
        public void testSplitName_RegularTwoParts() {
            ImmutablePair<String, String> result = StringUtil.splitName("John Doe");
            Assertions.assertEquals("John", result.left);
            Assertions.assertEquals("Doe", result.right);
        }

        @Test
        @Name("Split name with one part")
        public void testSplitName_SingleName() {
            Assertions.assertThrows(CustomExceptions.NameSplitException.class, () -> StringUtil.splitName("John"));
        }

        @Test
        @Name("Split name with prefixed last name")
        public void testSplitName_WithPrefix() {
            ImmutablePair<String, String> result = StringUtil.splitName("Juan de Marco");
            Assertions.assertEquals("Juan", result.left);
            Assertions.assertEquals("De Marco", result.right);
        }

        @Test
        @Name("Split name with prefixed last name and multiple names")
        public void testSplitName_WithPrefixAndMultipleNames() {
            ImmutablePair<String, String> result = StringUtil.splitName("Ginige Oneli de Silva");
            Assertions.assertEquals("Ginige", result.left);
            Assertions.assertEquals("De Silva", result.right);
        }

        @Test
        @Name("Split name with multiple names")
        public void testSplitName_MultipleNames() {
            ImmutablePair<String, String> result = StringUtil.splitName("Albus Severus James Potter");
            Assertions.assertEquals("Albus", result.left);
            Assertions.assertEquals("Potter", result.right);
        }

        @Test
        @Name("Split name with empty or null name")
        public void testSplitName_EmptyOrNull() {
            Assertions.assertThrows(CustomExceptions.NameSplitException.class, () -> StringUtil.splitName(""));
            Assertions.assertThrows(CustomExceptions.NameSplitException.class, () -> StringUtil.splitName(null));
        }

        @Test
        @Name("Parse phone number without country code + 0")
        public void testParsePhoneNumber_RegularStartsWithZero() {
            String phoneNumber = "0123456789";
            String expected = "+94123456789";
            Assertions.assertEquals(expected, StringUtil.parsePhoneNumber(phoneNumber));
        }

        @Test
        @Name("Parse phone number without country code")
        public void testParsePhoneNumber_RegularDoesNotStartWithZero() {
            String phoneNumber = "123456789";
            String expected = "+94123456789";
            Assertions.assertEquals(expected, StringUtil.parsePhoneNumber(phoneNumber));
        }

        @Test
        @Name("Parse phone number with country code")
        public void testParsePhoneNumber_WithCountryCode() {
            String phoneNumber = "+94123456789";
            Assertions.assertEquals(phoneNumber, StringUtil.parsePhoneNumber(phoneNumber));
        }

        @Test
        public void testParsePhoneNumber_WithSpaces() {
            String phoneNumber = "01 234 56789";
            String expected = "+94123456789";
            String phoneNumber2 = "+94 77 953 7175";
            String expected2 = "+94779537175";
            String phoneNumber3 = "\u202A+94 74 325 2075\u202C";
            String expected3 = "+94743252075";

            Assertions.assertEquals(expected, StringUtil.parsePhoneNumber(phoneNumber));
            Assertions.assertEquals(expected2, StringUtil.parsePhoneNumber(phoneNumber2));
            Assertions.assertEquals(expected3, StringUtil.parsePhoneNumber(phoneNumber3));
        }

        @Test
        public void testParsePhoneNumber_NullPhoneNumber() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parsePhoneNumber(null));
        }

        @Test
        public void testParsePhoneNumber_EmptyString() {
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parsePhoneNumber(""));
        }

        @Test
        public void testParsePhoneNumber_InvalidLength() {
            String phoneNumber = "012345678";
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parsePhoneNumber(phoneNumber));
        }

        @Test
        public void testParsePhoneNumber_InvalidCountryCode() {
            String phoneNumber = "+9412345678"; // incorrect number of digits
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parsePhoneNumber(phoneNumber));
        }

        @Test
        public void testParseGender() {
            String gender1 = "M";
            String expected1 = "Male";
            Assertions.assertEquals(expected1, StringUtil.parseGender(gender1));
            String gender2 = "F";
            String expected2 = "Female";
            Assertions.assertEquals(expected2, StringUtil.parseGender(gender2));
            String gender3 = "Other";
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parseGender(gender3));
            String gender4 = " ";
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parseGender(gender4));
            String gender5 = null;
            Assertions.assertThrows(IllegalArgumentException.class, () -> StringUtil.parseGender(gender5));
        }

        @Test
        public void testCapitalizeName() {
            String name1 = "john doe";
            String expected1 = "John Doe";
            Assertions.assertEquals(expected1, StringUtil.capitalizeName(name1));
            String name2 = "JOHN DOE";
            String expected2 = "John Doe";
            Assertions.assertEquals(expected2, StringUtil.capitalizeName(name2));
            String name3 = "yuwanath de silva";
            String expected3 = "Yuwanath De Silva";
            Assertions.assertEquals(expected3, StringUtil.capitalizeName(name3));
        }


    }
}
