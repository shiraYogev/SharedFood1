package com.example.sharedfood;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.runners.JUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(JUnit4.class) // insted of @RunWith(AndroidJUnit4.class)
public class ManagePostsActivityTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private Task<Void> mockDeleteTask;

    @Mock
    private Task<QuerySnapshot> mockQueryTask;

    private ManagePostsActivity managePostsActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        managePostsActivity = new ManagePostsActivity();
        managePostsActivity.setDb(mockFirestore); // אם יצרת Setter
    }

    @Test
    public void testDeletePost_Success() {
        // יצירת פוסט לדוגמא
        Post post = new Post();
        post.setId("testPostId");

        // הדמיית מחיקה מוצלחת
        when(mockDeleteTask.isSuccessful()).thenReturn(true);
        when(mockFirestore.collection("posts").document(post.getId()).delete()).thenReturn(mockDeleteTask);


        // קריאה לפונקציה למחיקת פוסט
        managePostsActivity.deletePost(post);

        // וידוא שה-Firebase אכן ביצע קריאה למחיקת הפוסט
        verify(mockFirestore.collection("posts").document(post.getId())).delete();
    }

    @Test
    public void testDeletePost_Failure() {
        // יצירת פוסט לדוגמא
        Post post = new Post();
        post.setId("invalidPostId");

        // הדמיית כישלון במחיקה
        when(mockFirestore.collection("posts").document(post.getId()).delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.isSuccessful()).thenReturn(false);

        // קריאה לפונקציה למחיקת פוסט
        managePostsActivity.deletePost(post);

        // בדיקה שהתקבלה שגיאה בעת מחיקת פוסט
        verify(mockFirestore.collection("posts").document(post.getId())).delete();
    }

    @Test
    public void testDeletePost_TriggersLoadAllPosts() {
        // יצירת פוסט לדוגמא
        Post post = new Post();
        post.setId("testPostId");

        // הדמיית מחיקה מוצלחת
        when(mockFirestore.collection("posts").document(post.getId()).delete()).thenReturn(mockDeleteTask);
        when(mockDeleteTask.isSuccessful()).thenReturn(true);

        // יצירת מרגל על פונקציית loadAllPosts()
        ManagePostsActivity spyActivity = spy(managePostsActivity);
        doNothing().when(spyActivity).loadAllPosts();

        // קריאה לפונקציה למחיקת פוסט
        spyActivity.deletePost(post);

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 // בדיקה שהפונקציה loadAllPosts() הופעלה אחרי מחיקת פוסט
        verify(spyActivity).loadAllPosts();
    }
}
