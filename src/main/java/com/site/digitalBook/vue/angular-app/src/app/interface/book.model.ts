import { Categorie } from "./categorie.model";

export interface Book {
  id: number;
  titre: string;
  auteur: string;
  prix: number;
  stock: number;
  remise: number;
  ISBN: string;
  editeur: string;
  datePublication: Date;
  estVendable: boolean;
  photos: string[];
  categories: Categorie[];  // Liste de catégories auxquelles le livre appartient
  enPromotion?: boolean;    // Ajout de cette propriété pour la promotion
  disponible?: boolean;    // Ajout de cette propriété pour la disponibilité
}
