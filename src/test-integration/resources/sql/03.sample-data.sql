INSERT INTO integration.accounts(email, public_id, password, salt, reminder)
VALUES
(
  'dominik.krenski@gmail.com',
  'cee0fa30-d170-4d9c-af8a-93ab159e9532',
  '$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOjjftBToa22',
  '711882a4dc3dcb437eb6151c09025594',
  'taka sobie prosta wiadomość'

),
(
  'dorciad@interia.pl',
  'e455b70f-50c5-4a96-9386-58f6ab9ba24b',
  '$2a$12$1rCLWvFfj1lcHm2lP1MJ/OyTNFseGh.mVdAGinD1gaOzzftBToa22',
  '711882a4dc3dcb437eb6151c01225594',
  null
),
(
  'dominik@yahoo.com',
  'f01048b2-622a-49b6-963e-5e8edeec8026',
  '$2a$12$1rCLEvFfj1lcHm2lP1NJ/OyTNFseFh.mVdAGinD1gaOzzftBToa38',
  '745882a4dc3dcd437ebef51c11225594',
  'przykładowa przypominajka'
);

INSERT INTO integration.data(public_id, entry, type, account_id)
VALUES
(
  '84ab5b68-2fa4-44eb-bd49-c5ab44eac6cd',
  'entry_1',
  'ADDRESS',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  'ec28a035-a31b-461d-9fcd-70c9982c1a22',
  'entry_2',
  'ADDRESS',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '9f569e10-64e1-4493-99e0-6a988a232e6b',
  'entry_3',
  'PASSWORD',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '9bfc99d8-8bf3-45e4-b8ee-4c286408ac29',
  'entry_4',
  'PASSWORD',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '05618eec-dc25-4c24-b908-4fce6cb04ad4',
  'entry_5',
  'SITE',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '3299f2fe-f930-44b6-8b10-c23c2efe5d1f',
  'entry_6',
  'SITE',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '67f9c86f-36eb-4fab-94ac-f68d113ad9d7',
  'entry_7',
  'NOTE',
   (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  '67a09bd7-5d26-4532-9758-9be4fa5a58c6',
  'entry_8',
  'NOTE',
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  'c4abe5cf-b30f-4a0e-8da1-6cdee6cad35a',
  'entry_9',
  'ADDRESS',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
),
(
  '13b2b31d-aa13-4cab-82dd-c979f6e8d1fe',
  'entry_10',
  'ADDRESS',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
),
(
  'f469ec9c-5041-4a1f-b840-69f3ae66c1ac',
  'entry_11',
  'PASSWORD',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
),
(
  'c3a175d3-cb93-4418-a480-eaee21505a49',
  'entry_12',
  'SITE',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
),
(
  'd088d359-c608-4d76-bf2d-60e0ec0f8fb7',
  'entry_12',
  'SITE',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
),
(
  'c87b861c-eff2-4d59-aa0c-232c5d7ee181',
  'entry_13',
  'NOTE',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
),
(
  'f4ed5604-667e-4395-9461-59b9a356b431',
  'entry_13',
  'NOTE',
  (SELECT id FROM integration.accounts WHERE email = 'dorciad@interia.pl')
)