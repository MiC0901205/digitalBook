// commande.model.ts
export interface Commande {
    user: { id: number };
    prixTotal: number;
    methodePaiement: string;
    dateCreation: string; // Utilisez le format ISO string pour la date
    livreIds: number[];
  }
  