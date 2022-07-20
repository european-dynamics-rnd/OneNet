db.createUser({
  user: 'onenet-operation',
  pwd: 'true2022-operation',
  roles: [
    {
      role: 'readWrite',
      db: 'orion',
    },
  ],
});
 