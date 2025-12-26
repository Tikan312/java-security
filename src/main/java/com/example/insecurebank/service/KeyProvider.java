package com.example.insecurebank.service;

import javax.crypto.SecretKey;


public interface KeyProvider {


    SecretKey getEncryptionKey();
}