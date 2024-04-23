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
}