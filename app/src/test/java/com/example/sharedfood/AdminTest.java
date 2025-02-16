package com.example.sharedfood;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.sharedfood.Admin;

import org.mockito.Mockito;

public class AdminTest {

    @Test
    public void testAdminConstructorAndGetters() {
        // הגדרת התנאים לבדיקה
        String testEmail = "test@example.com";
        boolean isSuperAdmin = true;

        // קריאה לפונקציה הנבדקת
        Admin admin = new Admin(testEmail, isSuperAdmin);

        // Assertions כדי לבדוק שהתוצאה תואמת לציפיות
        assertEquals(testEmail, admin.getEmail());
        assertTrue(admin.isSuperAdmin());
    }

    @Test
    public void testAdminIsNotSuperAdmin() {
        // הגדרת התנאים לבדיקה
        String testEmail = "test@example.com";
        boolean isSuperAdmin = false;

        // קריאה לפונקציה הנבדקת
        Admin admin = new Admin(testEmail, isSuperAdmin);

        // Assertions כדי לבדוק שהתוצאה תואמת לציפיות
        assertEquals(testEmail, admin.getEmail());
        assertFalse(admin.isSuperAdmin());
    }


    @Test
    public void testWithMockito() {
        // הגדרת התנאים לבדיקה
        String testEmail = "mock@example.com";
        Admin mockAdmin = Mockito.mock(Admin.class);

        // קריאה לפונקציה הנבדקת - כאן אנחנו מגדירים את התנהגות המוק לפונקציות
        Mockito.when(mockAdmin.getEmail()).thenReturn(testEmail);
        Mockito.when(mockAdmin.isSuperAdmin()).thenReturn(true);

        // Assertions כדי לבדוק שהתוצאה תואמת לציפיות
        assertEquals(testEmail, mockAdmin.getEmail());
        assertTrue(mockAdmin.isSuperAdmin());
    }


}