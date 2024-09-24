// carte-de-paiement.model.ts
export interface PaymentCard {
    id?: number; // Peut être optionnel lors de la création
    cardNumber: string;
    expiryDate: string;
    cvv: string;
    userId: number; // ID de l'utilisateur associé à la carte
  }
  