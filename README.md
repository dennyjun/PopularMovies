# Introduction
Popular movies uses theMovieDB API to display movies in a grid arrangement based on the sort settings specified by the user. The user can view the details of a displayed movie, read user reviews, watch trailers and manage favorites. This application is optimized for both phones and tablets.  

# Configuration
The application will not work until an API key is included in the manifest file. Fill in the android:value field in the AndroidManifest.xml file.

        <meta-data android:name="org.themoviedb.api.key" android:value="" />
