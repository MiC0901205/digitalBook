import { Categorie } from "./categorie.model";

export interface Book {
  id: number;
  titre: string;
  auteur: string;
  prix: number;
  remise: number;
  isbn: string;
  editeur: string;
  datePublication: Date;
  estVendable: boolean;
  photos: string; 
  categories: Categorie[];
  description: string;  
}

