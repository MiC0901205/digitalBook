import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentCard } from '../../interface/paymentCard.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Fonction pour récupérer les cartes enregistrées de l'utilisateur
  getSavedCards(userId: number): Observable<PaymentCard[]> {
    return this.http.get<PaymentCard[]>(`${this.apiUrl}/payment-cards/${userId}`);
  }

  // Fonction pour envoyer les données de carte de paiement
  sendPaymentCard(cardNumber: string, expiryDate: string, cvv: string, userId: number): Observable<any> {
    // Convertir les chaînes en Base64
    const cardNumberBase64 = btoa(cardNumber);
    const cvvBase64 = btoa(cvv); 

    // Préparer les données à envoyer
    const paymentData = {
        cardNumber: cardNumberBase64, 
        expiryDate: expiryDate,       
        cvv: cvvBase64,
        userId: userId 
    };

    return this.http.post(`${this.apiUrl}/payment-cards`, paymentData);
  }

  // Fonction pour supprimer une carte de paiement par son ID
  deletePaymentCard(cardId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/payment-cards/${cardId}`);
  }
}
