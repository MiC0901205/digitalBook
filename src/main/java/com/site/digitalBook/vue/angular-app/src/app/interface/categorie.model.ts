export interface Categorie {
  id: number;
  name: string; 
  remise: number;
  remiseCumulable: boolean;
  parentId?: number;
  type?: string;    
  description?: string;
  selected?: boolean; 
}
