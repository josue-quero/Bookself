
# Bookself

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Tracks what books an individual has been reading, and recommends them other books based on their book interests. It allows searching for books through a search bar or by scanning an ISBN Barcode. It shows the details of books and allows the user to create collections of books and see the progress that he has on a certain book. In short, it's a reading manager app.


### Final Walkthrough

<img src='screenshots/walkthrough_1.gif' title='Video Walkthrough 1' width='' alt='Video Walkthrough 1' />  

<img src='screenshots/walkthrough_2.gif' title='Video Walkthrough 2' width='' alt='Video Walkthrough 2' />  

<img src='screenshots/walkthrough_3.gif' title='Video Walkthrough 3' width='' alt='Video Walkthrough 3' />  

### App Evaluation
- **Category:** Books/social
- **Mobile:** This app would be primarily developed for mobile but it could be just as viable on a computer, such as Google Books or other similar apps. Functionality wouldn't be limited to mobile devices, however mobile version could potentially have more features.
- **Story:** analyzes what books an individual has been reading, and recommend them other books based on their book interests. They user can decide to accept these recommendations or simply ignore them and use the app to follow their reading progress.
- **Market:** This is a very nice app, for the people that enjoy books and want to be more consistent with their reading.
- **Habit:** This app could be used as often or as seldom as the user wants depending on the time the user decides to dedicate to reading books.
- **Scope:** First, I would start with recommending books based on book taste, then perhaps this could evolve into a library locator application as well to broaden its usage. Large potential for use in collages and public libraries.


## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can search for any given book (search bar).
* User can click on any book and get the details of said book.
* User can add a certain book to their shelf of owned books.
* User can establish on what page they're on for any given book they own.
* User can see the progress they have on the books they are currently reading.
* User gets recommended books based on their search history and reading tastes.
* User can log in and log out.
* User can create a new account.
* User gets prompted to select his favorite books.
* User can see the books he's currently reading in the profile tab.

**Optional Nice-to-have Stories**

* User can add a book to his library by taking a picture of the book.
* User can add a book to his library by taking a picture of the ISBN code of said book.
* User can click on a given book's author and get their bio information.
* User can see recommended books, organized by specific categories (genre, popular, public domain, etc.).
* User can see his book reading habit stats on the profile tab (amount of pages read, the amount of books read, all authors they have read, friends recommended books, etc.).
* User can edit a certain shelf (title, books that belong, etc.).
* User can create new shelves from the library tab.
* User can see the profile of their Facebook friends.

### 2. Screen Archetypes

* Launch screen
* Login.
    * User can log in and log out.
* Register screen
    * User can create a new account.
* Initial info screen
    * User gets prompted to select his favourite books.
* Discover screen
    * User gets recommended books based on their search history and reading tastes.
    * User can search for any given book (search bar).
    * User can see recommended books, organized by specific categories (genre, popular, public domain, etc.). (optional)
* Library screen
    * User can add a certain book to their shelf of owned books.
    * User can establish on what page they're on for any given book they own.
    * User can see the progress they have on the books they are currently reading.
    * User can add a book to his library by taking a picture of the book. (optional)
    * User can add a book to his library by taking a picture of the ISBN code of said book. (Optional)
    * User can edit a certain shelf (title, books that belong, etc.). (Optional)
    * User can create new shelves from the library tab. (optional)
* Profile screen
    * User can see the books he's currently reading in the profile tab.
    * User can see the profile of their Facebook friends. (Optional)
    * User can see his book reading habit stats on the profile tab (amount of pages read, the amount of books read, all authors they have read, friends recommended books, etc.). (Optional)
* Results screen
    * User can search for any given book (search bar).
### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Discover tab
* Library tab
* Profile tab

**Flow Navigation** (Screen to Screen)

* Forced log in -> Account creation if no log in is available
* Favorite books selection -> Discover screen
* Library screen -> All bookshelves
* Search bar -> Results screen
* Profile screen -> Open current reading books

### 4. API's

**Google Sign-In**

* More info available at: (https://developers.google.com/identity/sign-in/android/start-integrating)

**Google Books API**

* More info available at: (https://developers.google.com/books)

**ML Kit: Barcode Scanning**

* More info available at: (https://developers.google.com/ml-kit/vision/barcode-scanning)

## Wireframes

<img src="https://i.imgur.com/agRz8wH.png" width=300>  

<img src="https://i.imgur.com/CsxLosg.png" width=300>  

<img src="https://i.imgur.com/pm2DAQQ.png" width=300>  

<img src="https://i.imgur.com/9S9amzS.png" width=300>  

<img src="https://i.imgur.com/1VjkKxY.png" width=300>  

<img src="https://i.imgur.com/XRUyYia.png" width=300>  

<img src="https://i.imgur.com/dvqYtJe.png" width=300>  

<img src="https://i.imgur.com/i72PZud.png" width=300>  

<img src="https://i.imgur.com/FVrdkwe.png" width=300>  

<img src="https://i.imgur.com/62r8MhO.png" width=300>  

<img src="https://i.imgur.com/UQmwhK3.png" width=300>  

<img src="https://i.imgur.com/ijDoOyI.png" width=300>  

<img src="https://i.imgur.com/mw0gMjV.png" width=300>  

## Schema ### Models
#### Books
| Property      | Type     | Description |  
| ------------- | -------- | ------------| | objectId      | String   | unique id for the user post (default field) |  
| googleObjectId| String   | API's unique id for the user's book |  
| bookTitle     | String   | book's title |  
| authors       | Array of Pointers to Authors| book's authors |  
| image         | File     | cover picture of book |  
| publisher     | String   | name of the publisher |  
| synopsis      | String   | synopsis of the book |  
| releaseYear   | String   | year the book was originally published |  
| rating        | Number   | avrg rating of the book |  
| length        | Number   | amount of pages in the book |  
| category      | Array of strings | genres the book belongs to |  
| createdAt     | DateTime | date when book is created (default field) |  
| updatedAt     | DateTime | date when book is last updated (default field) |
#### Users

| Property      | Type     | Description |  
| ------------- | -------- | ------------| | objectId      | String   | unique id for the user (default field) |  
| username      | String   | name of the user |  
| password     | String   | password of user's account |  
| userBooks     | Array of JSON Object | user's books |  
| monthlyBooks  | Number   | amount of books the user wants to read every month |  
| pagesRead     | Number   | amount of pages the user has read |  
| booksRead     | Number   | amount of books the user has read |  
| shelfs        | Array of Pointers to shelfs | user's personalized libraries |  
| lastreadAt    | DateTime | most recent date the user read |  
| createdAt     | DateTime | date when book is created (default field) |  
| updatedAt     | DateTime | date when book is last updated (default field) |
#### Shelfs

| Property      | Type     | Description |  
| ------------- | -------- | ------------| | objectId      | String   | unique id for the user (default field) |  
| name          | String   | shelf's name |  
| createdAt     | DateTime | date when book is created (default field) |  
| updatedAt     | DateTime | date when book is last updated (default field) |
##### User's books


| Property      | Type     | Description |  
| ------------- | -------- | ------------| | book          | Pointer to Book | book object |  
| favorited     | Boolean  | true if user has favorited this book |  
| lastPage      | Number   | last page where it was left at |  
| readAt        | DateTime | most recent date the book was read |  
| shelfs        | Array of Pointers to Shelfs | shelfs to where this book belongs |


### Networking
- Recommendation screen  
  - (Read/GET) Query all books that match the user's search  
  - (Read/GET) Query all books from the "Books for you" bookshelf.  
  - (Update/PUT) Add book to user's owned bookshelf  
  - (Update/PUT) Add book to user's shelf
- Library screen  
  - (Create/POST) Create a new shelf  
  - (Update/PUT) Add book to user's owned bookshelf  
  - (Update/PUT) Add book to user's shelf  
  - (Delete) Delete existing shelf
- Profile Screen  
  - (Read/GET) Query logged in user object  
  - (Read/GET) Query all books from the "Reading" bookshelf.  
  - (Update/PUT) Update user profile image