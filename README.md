# Flappy - Fly Above the World

A Social Media App with enormous features built with Kotlin that uses Appwrite BaaS for User Authentication, Realtime Database and Storage. The app follows clean MVVM architecture principles and uses Koin for dependency injection.

## 🚀 Features

* Allow user to sign up and log in.

* Home Page - Displays followed users, followed hashtags and random flaps / posts. The posts from your followed user and hashtags will always be shown, apart from them, there will be some random posts.

* Search Page - Search for tweets containing searched hashtag. You may follow hashtag by clicking follow button. If you have followed a hashtag, any new post made with the hashtag will be shown on your home page.

* My Activity Page - Shows your created or supported flaps sorted by date modified. Any flap supported by you, will be shown on your activity page with current date to it.

* Current User Profile Page - See your profile with your visible name, your under 10 characters username (you can change it or it will be generated uniquely), your posts, followers, users following, about section and contact information (mobile and email). 

* Selected User Profile Page - You can see the profile of the person who has posted any flap just by clicking on his name on the flap. It will contain user information similar to you (name, username, post numbers, followers, following, about and contact info). You can also follow and unfollow him over there.

* Comment Page - User when clicks the comment icon on any flap, he'll be sent to a page where he can comment on the flap. It will also show the date when he commented.

* Create tweets adding images of your choice (size less than 3 MB).

* Like, Support, Comment & Delete functionality for flaps.

* Dependency Injection using Koin for better architecture.

## Important Note
* Any issue like unable to update profile image, unable to see other's profile, etc. occur only if you have just signed up and using the app. If you by any chance face any of the issues, try signing out and then logging in again.

* While updating user profile, make sure you enter phone number in the format: (country_code)(number).

* While updating user profile, make sure that username is not more than 10 characters.

* Any user with more than 10 followers will be SKY user. This name is given to those who are popular members on the platform. Rest users will be SURFACE users. This will be displayed on every user's profile with a badge alongside representing their popularity.

## 🛠️ Tech Stack

Language: Kotlin

UI: Android XML + Jetpack Compose

Dependency Injection: Koin

Backend: Appwrite

## 📸 Screenshots

Login: 
<img width="668" height="1486" alt="image" src="https://github.com/user-attachments/assets/19b3b659-3ed6-498a-a0fa-dd3e504c8651" />



## 🔧 Installation

Clone the repository:

git clone https://github.com/crimsonyash9012/Flappy

Open the project in Android Studio.

Build and run the app on an emulator or physical device.

APK Download Link: https://www.mediafire.com/file/i9dpcxf196cbbxy/twitter.apk/file


## 💡 Future Enhancements

* Editing profile name.

* Show popular and trending hashtags and users.
  
* Push Notifications.
  
* Reply to comments.
  
* Personal messaging.
  
* Video support in flaps.
