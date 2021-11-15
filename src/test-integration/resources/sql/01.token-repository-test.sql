INSERT INTO integration.accounts(email, password, salt, reminder)
VALUES
('dominik.krenski@gmail.com', 'b468879149f241f69ce185ee2cc1764047ece00f7aad0128053a12aee5be320c', '711882a4dc3dcb437eb6151c09025594', 'simple reminder'),
('dorciad@interia.pl', 'c468879149f241f69ce185ee2cc1764047ece00f7aad0528053a12aee5be320c', '741882a4dc3dcb437eb6151d09025f94', null);

INSERT INTO integration.refresh_tokens(token, used, account_id)
VALUES
(
  'refresh_token_1',
  false,
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  'refresh_token_2',
  false,
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
),
(
  'refresh_token_3',
  false,
  (SELECT id FROM integration.accounts WHERE email = 'dominik.krenski@gmail.com')
)