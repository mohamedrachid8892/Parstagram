package com.example.parstagram

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.parse.*
import java.io.File


/**
 * Let the user create a post by taking a photo with their camera
 */
class MainActivity : AppCompatActivity() {

    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034
    val photoFileName = "photo.jpg"
    var photoFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Set the Description of the Post
        // 2. Button to launch the camera to take a picture
        // 3. ImageView to Show the picture the user has taken
        // 4. A button to save and send the post to our Parse Server

        val user = ParseUser.getCurrentUser()
        findViewById<TextView>(R.id.currUser).text = user.username.toString()


        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            // Send post to server without an image
            // Get the description that they have inputted
            val description = findViewById<EditText>(R.id.etDescription).text.toString()
            if (photoFile != null) {
                submitPost(description, user, photoFile!!)
                findViewById<Button>(R.id.btnSubmit).visibility = View.INVISIBLE

                val pb : ProgressBar = findViewById(R.id.pbLoading)
                pb.visibility = ProgressBar.VISIBLE
            } else {
                Log.e(TAG, "Error submitting post. File is empty")
                Toast.makeText(this, "Error submitting post. You didn't take a photo!", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnLogOut).setOnClickListener {
            ParseUser.logOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnTakePicture).setOnClickListener {
            // Launch camera to let user take a picture
            onLaunchCamera()
        }

//        queryPosts()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // at this point we have the camera photo on disk
                val takenImage = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                val ivPreview: ImageView = findViewById(R.id.ivPicture)
                ivPreview.setImageBitmap(takenImage)
            } else {
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Send a Post object to the server
    fun submitPost(description: String, user: ParseUser, file: File) {

        // Create the Post Object
        val post = Post()
        post.setDescription(description)
        post.setUser(user)
        post.setImage(ParseFile(file))
        post.saveInBackground { exception ->
            if (exception != null) {
                // Something has went wrong
                Log.e(TAG, "Error while saving post")
                exception.printStackTrace()
                Toast.makeText(this, "Something went wrong saving your post!", Toast.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "Post saved successfully")

                val pb : ProgressBar = findViewById(R.id.pbLoading)
                pb.visibility = ProgressBar.INVISIBLE
                findViewById<Button>(R.id.btnSubmit).visibility = View.VISIBLE

                // Reset the Description field
                findViewById<EditText>(R.id.etDescription).text.clear()

                // Reset the image view
                val ivPreview: ImageView = findViewById(R.id.ivPicture)
                ivPreview.setImageResource(android.R.color.transparent)

                // Display toast notifying post saved successfully
                Toast.makeText(this, "Post saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    fun getPhotoFileUri(fileName: String): File {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        val mediaStorageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory")
        }

        // Return the file target for the photo based on filename
        return File(mediaStorageDir.path + File.separator + fileName)
    }

    fun onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName)

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        if (photoFile != null) {
            val fileProvider: Uri =
                FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile!!)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
        }
    }

    // Query for all posts in the server
    fun queryPosts() {

        // Specify which class to query
        val query: ParseQuery<Post> = ParseQuery.getQuery(Post::class.java)
        // Find all Post Objects
        query.include(Post.KEY_USER)
        query.findInBackground(object: FindCallback<Post> {
            override fun done(posts: MutableList<Post>?, e: ParseException?) {
                if (e != null) {
                    Log.e(TAG, "Error fetching posts")
                } else {
                    if (posts != null) {
                        for (post in posts) {
                            Log.i(TAG, "Post: " + post.getDescription() +
                                    " , Username: " + post.getUser()?.username)
                        }
                    }
                }
            }
        })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}