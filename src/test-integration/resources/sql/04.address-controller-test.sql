INSERT INTO integration.accounts(email, public_id, password, salt, reminder)
VALUES
('dominik.krenski@gmail.com', 'd85db87a-df23-49e3-baef-8523e84902d1', '$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOjjftBToa22', '711882a4dc3dcb437eb6151c09025594', 'simple reminder'),
('dorciad@interia.pl', '983ce893-acc1-431c-b9e3-ffbe4394ef42', '$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOzzftBToa22', '711882a4dc3dcb437eb6151c01225594', null);

INSERT INTO integration.addresses(address, public_id, account_id)
VALUES
(
  '50d00dbe0817df9d676a8a2d.af3453c9',
  'cc7037fe-4b2d-44fc-a01d-fb196f3d2a82',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '50d00dbe0817df9d676a8adc.af3453c95',
  '498c0f91-955b-4c66-b6fb-7b1161f09561',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '50d00dbe0b07df9d676a8a2d.af3453c9',
  'b9a887bc-fb09-4c46-9380-3ac70489e4fa',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
);

