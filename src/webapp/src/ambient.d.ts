type ImageUris = {
    full: string;
    large: string;
    normal: string;
    small: string;
}

type CardFace = 'front' | 'back';

type CardLayout = 'NORMAL' | 'TRANSFORM' | 'TOKEN';

type IvionCardFaceData = {
    face: CardFace;
    name?: string | null;
    rulesText?: string | null;
    flavorText?: string | null;
    artist?: string | null;
    imageUris?: ImageUris | null;
}

type LinkedRelation = 'TOKEN' | 'GENERATED_BY' | 'TRANSFORMS_FROM' | 'TRANSFORMS_INTO';

type LinkedPart = {
    id: string;
    relation: LinkedRelation;
}

type IvionCard = {
    id: string;
    collectorsNumber: string | null;
    format: string | null;
    name: string;
	archetype: string | null;
	actionCost: number | null;
	powerCost: number | null;
	range: number | null;
	health: number | null;
	heroic: boolean;
	slow: boolean;
	silence: boolean;
	disarm: boolean;
	extraType: string | null;
	rulesText: string | null;
	flavorText: string | null;
	artist: string;
	ivionUUID: string;
	secondUUID: string | null;
	colorPip1: string | null;
    colorPip2: string | null;
    season: string;
    type: string | null;
    layout?: CardLayout | null;
    imageUris?: ImageUris | null;
    faces?: IvionCardFaceData[] | null;
    linkedParts?: LinkedPart[];
    herocraftId?: string | null;
    printVariantGroupId?: string | null;
    variants?: string[];
};
