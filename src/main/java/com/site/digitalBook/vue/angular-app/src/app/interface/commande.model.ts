export interface Commande {
  id?: number;
  user: { id: number }; // User est réduit à un objet contenant uniquement l'ID
  prixTotal: number;
  methodePaiement: string;
  dateCreation: string; // Date en format ISO string
  livreIds: number[];
}
