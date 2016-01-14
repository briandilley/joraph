test schema
======

Here's the details on this test schema:

```
_User_
id		pk
name

_UserEx_
id		pk
name

_Author_
id		pk
name

_Genre_
id				pk
name
description

_Library_
id					pk
name
librarianUserId

_Book_
id					pk
authorId			fk -> Author.id
genreId				fk -> Genre.id
libraryId			fk -> Library.id
name
isbn
numberOfPages

_SimilarBook_
id				pk
bookId			fk -> Book.id
similarBookId	fk -> Book.id
reason

_Checkout_
id			pk
libraryId	fk -> Library.id
userId		fk -> User.id
bookId		fk -> Book.id
```