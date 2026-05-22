<?php

class PaymentEngine
{
    public function processPayment(float $amount): bool
    {
        return $amount > 0;
    }
}