package com.example.cooperativevoting.eligibility.application;

public class InvalidCpfException extends RuntimeException {
  public InvalidCpfException() {
    super("O CPF informado é inválido.");
  }
}
