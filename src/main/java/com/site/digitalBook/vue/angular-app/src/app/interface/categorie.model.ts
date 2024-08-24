export interface Categorie {
  id: number;
  name: string;  // Utilisez 'nom' ici pour correspondre aux données
  remise: number;
  remiseCumulable: boolean;
  parentId?: number;  // Utilisez '?' pour indiquer que ce champ est optionnel
  type?: string;      // Assurez-vous que ce champ est optionnel si nécessaire
  description?: string;
  selected?: boolean; // Ajouté pour le filtre de sélection de catégorie
}
