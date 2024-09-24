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

  // Fonction pour convertir une chaîne de caractères en tableau de bytes (Uint8Array)
  private stringToByteArray(str: string): Uint8Array {
    return new TextEncoder().encode(str);
  }

  // Fonction pour convertir un tableau de bytes en chaîne Base64
  private byteArrayToBase64(byteArray: Uint8Array): string {
    let binary = '';
    const len = byteArray.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(byteArray[i]);
    }
    return btoa(binary);
  }

  // Fonction pour récupérer les cartes enregistrées de l'utilisateur
  getSavedCards(userId: number): Observable<PaymentCard[]> {
    return this.http.get<PaymentCard[]>(`${this.apiUrl}/payment-cards/${userId}`);
  }

  // Fonction pour envoyer les données de carte de paiement
  sendPaymentCard(cardNumber: string, expiryDate: string, cvv: string, userId: number): Observable<any> {
    // Convertir les chaînes en Base64
    const cardNumberBase64 = btoa(cardNumber); // Encode le numéro de carte
    const cvvBase64 = btoa(cvv);               // Encode le CVV

    // Préparer les données à envoyer
    const paymentData = {
        cardNumber: cardNumberBase64,  // Numéro de carte encodé en Base64
        expiryDate: expiryDate,        // Date d'expiration telle quelle
        cvv: cvvBase64,                // CVV encodé en Base64
        userId: userId                  // ID de l'utilisateur
    };

    console.log("Données envoyées au backend:", paymentData); // Log des données envoyées

    // Envoi de la requête POST à l'API backend
    return this.http.post(`${this.apiUrl}/payment-cards`, paymentData);
  }

  // Fonction pour supprimer une carte de paiement par son ID
  deletePaymentCard(cardId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/payment-cards/${cardId}`);
  }
}
