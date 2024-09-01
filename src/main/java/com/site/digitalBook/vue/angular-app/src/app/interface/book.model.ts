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
  categories: Categorie[];
  description: string;  
  pdfUrl?: string; 
}
