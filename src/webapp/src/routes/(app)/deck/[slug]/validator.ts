// The validator takes in a deck List, and returns a validation status. This status is either true or false, and if it is false, will contain a list of failed rules.
// Some cards may have an override rule.

import type { Deck, DeckEntry } from '../../../../app';
import type { CollatedDeckList } from './+page.ts';

type ValidationResult = {
	status: boolean;
	metadata?: any;
}

type OverrideRule = {
	overrideRule: string;
	rule: ValidationRule;
}

type ValidationRule = (deck: Deck, collatedCards: CollatedDeckList) => ValidationResult;

const validateCardCount = (
	deck: Deck,
	filterFn: (entry: any) => boolean,
	expectedCount: number,
	label: string
) => {
	let count = deck.list
		.filter(filterFn)
		.reduce((sum, entry) => sum + entry.count, 0);
	return {
		status: count === expectedCount,
		metadata: {
			count,
			label
		}
	};
};

const validateCopyLimits = (
	deck: Deck,
	filterFn: (entry: DeckEntry) => boolean,
	limit: number,
	label: string
) => {
	const invalidCards = deck.list
		.filter(filterFn)
		.filter((entry) => entry.count > limit);

	return {
		status: invalidCards.length === 0,
		metadata: { invalidCards, label }
	};
};

const validateClassCount = (
	collatedCards: CollatedDeckList,
	lowerBound: number,
	upperBound: number,
	label: string) => {
	let classes: Set<String> = new Set<String>;

	for (let [group, entries] of Object.entries(collatedCards)) {
		if (group !== 'Trait') {
			if (entries !== undefined && entries.length >= 1) {
				if (entries[0].card.type === 'Class') {
					classes.add(entries[0].card.archetype!!);
				}
			}
		}
	}

	return {
		status: classes.size >= lowerBound && classes.size <= upperBound,
		metadata: {
			classes: [...classes],
			label
		}
	};
};


const DeckHas45Cards: ValidationRule = (deck: Deck) =>
	validateCardCount(deck, (entry) => entry.card.format === 'Skill', 45, 'Exactly 45 cards in your deck.');

const SpecializationHas15Cards: ValidationRule = (deck: Deck) =>
	validateCardCount(deck, (entry) => entry.card.type === 'Spec', 15, 'Exactly 15 Specialization cards in your deck.');

const ClassHas30Cards: ValidationRule = (deck: Deck) =>
	validateCardCount(deck, (entry) => entry.card.type === 'Class', 30, 'Exactly 30 Class cards in your deck.');


const NoMoreThan3CopiesOfEachSkill: ValidationRule = (deck: Deck) =>
	validateCopyLimits(deck, (entry) => entry.card.format === 'Skill', 3, 'No more than 3 copies of each skill card in your deck.');

const NoMoreThan1CopyOfEachTrait: ValidationRule = (deck: Deck) =>
	validateCopyLimits(deck, (entry) => entry.card.format === 'Feat', 1, 'No more than 1 copy of each trait card in your deck.');

const Exactly3Traits: ValidationRule = (deck: Deck) => {
	const count = deck.list
		.reduce((sum, entry) => {
			if (entry.card.format === 'Feat') {
				return sum + entry.count;
			}
			return sum;
		}, 0);

	return {
		status: count === 3,
		metadata: { count, label: 'Exactly 3 traits in your deck.' }
	};
};

let TraitColorRequirementsMet: (_deck: Deck, collatedCards: CollatedDeckList) => ValidationResult
	= (_deck: Deck, collatedCards: CollatedDeckList) => {
	if (!collatedCards) {
		return {
			status: false,
			metadata: {
				error: 'No cards provided',
				label: 'All Trait Colors satisfied by Classes and Specializations.'
			}
		};
	}

	// Create a map to count occurrences of each provided color
	const providedColorCounts = new Map<string, number>();
	Object.entries(collatedCards)
		.filter(([key, value]) => key !== 'Trait' && value && value[0])
		.forEach(([, value]) => {
			if (value) {
				const card = value[0].card;
				if (card.colorPip1) providedColorCounts.set(card.colorPip1, (providedColorCounts.get(card.colorPip1) || 0) + 1);
				if (card.colorPip2) providedColorCounts.set(card.colorPip2, (providedColorCounts.get(card.colorPip2) || 0) + 1);
			}
		});

	// Gather counts of required colors from Trait cards
	const requiredColorCounts = new Map<string, number>();
	(collatedCards['Trait'] || [])
		.filter((entry) => entry.card.type !== 'Ultimate')
		.forEach((entry) => {
			const card = entry.card;
			if (card.colorPip1) requiredColorCounts.set(card.colorPip1, (requiredColorCounts.get(card.colorPip1) || 0) + 1);
			if (card.colorPip2) requiredColorCounts.set(card.colorPip2, (requiredColorCounts.get(card.colorPip2) || 0) + 1);
		});

	// Keep track of missing colors
	const missingColors: { color: string, required: number, provided: number }[] = [];

	// Validate that all required colors are satisfied by provided colors
	for (const [color, requiredCount] of requiredColorCounts.entries()) {
		const providedCount = providedColorCounts.get(color) || 0;
		if (providedCount < requiredCount) {
			missingColors.push({
				color,
				required: requiredCount,
				provided: providedCount
			});
		}
	}

	return {
		status: missingColors.length <= 0,
		metadata: {
			missingColors,
			label: 'All Trait Colors satisfied by Classes and Specializations.'
		}
	};

};


// When Dilettante is a selected trait, you may only have 1 copy of each class card in your deck
const DilettanteClassCardCountLimit: ValidationRule = (deck: Deck, _: CollatedDeckList) =>
	validateCopyLimits(deck, (entry) => entry.card.type === 'Class', 1, 'Only 1 copy of each class card in your deck. (From Dilettante)');

const DilettanteClassCountLimit: ValidationRule = (_deck: Deck, collatedCards: CollatedDeckList) =>
	validateClassCount(collatedCards, 1, 3, 'Between 1 and 3 classes. (From Dilettante)');

const DeckHas1or2Classes: ValidationRule = (_deck: Deck, collatedCards: CollatedDeckList) =>
	validateClassCount(collatedCards, 1, 2, 'Either 1 or 2 classes.');

// Savant
// During Character Creation, put six more Specialization cards and nine fewer Class cards into your deck.
// (Your deck size is reduced by 3.)
const SavantClassCardCountLimit: ValidationRule = (deck: Deck, _: CollatedDeckList) =>
	validateCardCount(deck, (entry) => entry.card.type === 'Class', 21, 'Exactly 21 Class cards in your deck. (From Savant)');

const SavantSpecCardCountLimit: ValidationRule = (deck: Deck, _: CollatedDeckList) =>
	validateCardCount(deck, (entry) => entry.card.type === 'Spec', 21, 'Exactly 21 Specialization cards in your deck. (From Savant)');

const SavantCardCountLimit: ValidationRule = (deck: Deck, _: CollatedDeckList) =>
	validateCardCount(deck, (entry) => entry.card.format === 'Skill', 42, 'Exactly 42 cards in your deck. (From Savant)');

const validationRules = new Map<string, ValidationRule>([
	['DeckCardCount', DeckHas45Cards],
	['SpecializationCardCount', SpecializationHas15Cards],
	['ClassCardCount', ClassHas30Cards],
	['ClassCount', DeckHas1or2Classes],
	['SkillCardCopyCount', NoMoreThan3CopiesOfEachSkill],
	['TraitCardCopyCount', NoMoreThan1CopyOfEachTrait],
	['TraitCount', Exactly3Traits],
	['TraitRequirements', TraitColorRequirementsMet]
]);

const overrideRules = new Map<string, OverrideRule[]>([
	['Dilettante', [
		{ overrideRule: 'ClassCount', rule: DilettanteClassCountLimit },
		{ overrideRule: 'ClassCardCopyCount', rule: DilettanteClassCardCountLimit }
	]],
	['Savant', [
		{ overrideRule: 'DeckCardCount', rule: SavantCardCountLimit },
		{ overrideRule: 'SpecializationCardCount', rule: SavantSpecCardCountLimit },
		{ overrideRule: 'ClassCardCount', rule: SavantClassCardCountLimit }
	]]
]);

export let validateDeck = (deck: Deck, collatedCards: CollatedDeckList) => {
	if (deck === undefined || deck.list === undefined) {
		return {
			status: false
		};
	}

	let validationStatusT = validationRules.entries()
		.map(([key, rule]) => [key, rule(deck, collatedCards)]).toArray();
	// @ts-ignore
	let validationStatuses = new Map<string, ValidationResult>(validationStatusT);


	const overrides = deck.list.flatMap(entry =>
		overrideRules.get(entry.card.name)
	);

	overrides.forEach(override => {
		if (override !== undefined) {
			const result = override.rule(deck, collatedCards);
			validationStatuses.set(override.overrideRule, result);
		}
	});

	let resultMetadata: {
		status: boolean;
		metadata: any[];
	} = {
		status: true,
		metadata: []
	};

	for (const [ruleName, validationStatus] of validationStatuses) {
		let metadata = resultMetadata.metadata;
		if (!validationStatus.status) {
			metadata.push({ ruleName, metadata: validationStatus.metadata });
		}
		resultMetadata = {
			status: resultMetadata.status && validationStatus.status,
			metadata
		};
	}

	return { validationStatuses: validationStatuses, results: resultMetadata };

};