package com.example.sharedfood;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;

import com.example.sharedfood.admin.Admin;
import com.example.sharedfood.post.Post;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;

public class UnitTests {

    private Post post;
    private Admin admin;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollectionRef;

    @Mock
    private DocumentReference mockDocRef;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        List<String> filters = Arrays.asList("Vegetarian", "Gluten-Free");
        post = new Post("Delicious homemade meal", filters, "http://image.url");
        admin = new Admin("admin@example.com", true);

        Mockito.when(mockFirestore.collection("posts")).thenReturn(mockCollectionRef);
        Mockito.when(mockCollectionRef.document("post123")).thenReturn(mockDocRef);
    }

    @Test
    public void testPostInitialization() {
        assertEquals("Delicious homemade meal", post.getDescription());
        assertEquals(2, post.getFilters().size());
        assertEquals("http://image.url", post.getImageUrl());
    }

    @Test
    public void testAdminSuperAdminStatus() {
        assertTrue(admin.isSuperAdmin());
    }

    @Test
    public void testFirestoreMock() {
        try (MockedStatic<FirebaseFirestore> mockedFirestore = Mockito.mockStatic(FirebaseFirestore.class)) {
            mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
            assertNotNull(FirebaseFirestore.getInstance().collection("posts").document("post123"));
        }
    }
}
