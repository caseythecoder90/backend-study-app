db = db.getSiblingDB('flashcards');

db.createUser({
  user: 'flashcards_user',
  pwd: 'flashcards_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'flashcards',
    },
  ],
});

db.createCollection('users');
db.createCollection('decks');
db.createCollection('flashcards');
db.createCollection('study_sessions');