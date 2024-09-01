export interface User {
    id: number;
    nom: string;
    prenom: string;
    email: string;
    tel?: string;
    dateNaissance?: Date;
    estActif?: boolean;
    profil?: string;
    questionSecrete?: string;
    reponseSecrete?: string;
}
  