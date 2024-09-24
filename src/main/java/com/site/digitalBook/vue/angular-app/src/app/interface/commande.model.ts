export interface Commande {
  id?: number;
  user: { id: number }; 
  prixTotal: number;
  methodePaiement: string;
  dateCreation: string; 
  livreIds: number[];
}
