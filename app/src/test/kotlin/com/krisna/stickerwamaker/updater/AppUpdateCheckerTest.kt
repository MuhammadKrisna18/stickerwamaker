package com.krisna.stickerwamaker.updater

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckerTest {

    @Test
    fun isVersionGreater_newerPatch_returnsTrue() {
        assertTrue(AppUpdateChecker.isVersionGreater("1.0.1", "1.0.0"))
    }

    @Test
    fun isVersionGreater_newerMinor_returnsTrue() {
        assertTrue(AppUpdateChecker.isVersionGreater("1.1.0", "1.0.9"))
    }

    @Test
    fun isVersionGreater_newerMajor_returnsTrue() {
        assertTrue(AppUpdateChecker.isVersionGreater("2.0.0", "1.9.9"))
    }

    @Test
    fun isVersionGreater_sameVersion_returnsFalse() {
        assertFalse(AppUpdateChecker.isVersionGreater("1.0.0", "1.0.0"))
    }

    @Test
    fun isVersionGreater_olderVersion_returnsFalse() {
        assertFalse(AppUpdateChecker.isVersionGreater("0.9.5", "1.0.0"))
    }

    @Test
    fun isVersionGreater_differentSegmentLengths() {
        assertTrue(AppUpdateChecker.isVersionGreater("1.0.1", "1.0"))
        assertFalse(AppUpdateChecker.isVersionGreater("1.0", "1.0.1"))
    }
}

