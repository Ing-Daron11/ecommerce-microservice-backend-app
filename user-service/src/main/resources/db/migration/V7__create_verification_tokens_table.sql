
CREATE TABLE verification_tokens (
	verification_token_id INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
	credential_id INT(11),
	verif_token VARCHAR(255),
	expire_date DATE,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  	updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);

