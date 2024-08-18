export interface User {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    tel?: string;
    dateNaissance?: Date;
    estActif?: boolean;
    profil?: string;
    panier?: number;
    questionSecrete?: string;
    reponseSecrete?: string;
}
  