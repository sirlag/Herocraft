/**
 * Download utilities for card images and file handling
 */

/**
 * Sanitizes a string for use as a filename by removing special characters
 */
export function sanitizeFilename(s: string): string {
	return s
		.replace(/[^a-z0-9\-_.()\[\]\s]/gi, '')
		.replace(/\s+/g, ' ')
		.trim();
}

/**
 * Builds a filename for a card image
 */
export function buildCardFilename(card: IvionCard, face?: 'front' | 'back'): string {
	const base = sanitizeFilename(card.name || 'card');
	const facePart = face ? `-${face}` : '';
	const idPart = card.id ? `-${card.id}` : '';
	return `${base}${facePart}${idPart}.png`;
}

/**
 * Formats a byte count into a human-readable string (e.g., "1.5 MB")
 */
export function formatBytes(bytes: number): string {
	if (!Number.isFinite(bytes) || bytes < 0) return '';
	const units = ['B', 'KB', 'MB', 'GB', 'TB'];
	let i = 0;
	let v = bytes;
	while (v >= 1024 && i < units.length - 1) {
		v = v / 1024;
		i++;
	}
	// Keep one decimal for KB and above, no decimals for bytes
	const fixed = i === 0 ? Math.round(v).toString() : v.toFixed(v >= 100 ? 0 : 1);
	return `${fixed} ${units[i]}`;
}

/**
 * Fetches the content length of a remote resource using HEAD or Range requests
 */
export async function fetchContentLength(
	url: string,
	signal?: AbortSignal
): Promise<number | null> {
	try {
		// Try a HEAD request first
		const head = await fetch(url, { method: 'HEAD', mode: 'cors', redirect: 'follow', signal });
		const cl = head.headers.get('content-length');
		if (cl) {
			const n = Number(cl);
			if (Number.isFinite(n)) return n;
		}
	} catch {
		// Ignore and try range request fallback
	}

	try {
		// Fallback: request a single byte using Range and parse Content-Range total size
		const r = await fetch(url, {
			method: 'GET',
			headers: { Range: 'bytes=0-0' },
			mode: 'cors',
			redirect: 'follow',
			signal
		});
		const cr = r.headers.get('content-range');
		// Format: bytes 0-0/12345
		if (cr && /\/(\d+)\s*$/.test(cr)) {
			const m = cr.match(/\/(\d+)\s*$/);
			const total = m && m[1] ? Number(m[1]) : NaN;
			if (Number.isFinite(total)) return total;
		}
		const cl2 = r.headers.get('content-length');
		if (cl2) {
			const n = Number(cl2);
			if (Number.isFinite(n)) return n;
		}
	} catch {
		// Give up
	}
	return null;
}

/**
 * Forces download of a file from a URL by fetching as blob and triggering download
 * Falls back to opening in new tab if blob download fails
 */
export async function forceDownload(url: string, fileName: string): Promise<void> {
	try {
		const res = await fetch(url, { mode: 'cors' });
		if (!res.ok) throw new Error(`Download failed: ${res.status}`);
		const blob = await res.blob();
		const objectUrl = URL.createObjectURL(blob);
		const a = document.createElement('a');
		a.href = objectUrl;
		a.download = fileName;
		document.body.appendChild(a);
		a.click();
		a.remove();
		URL.revokeObjectURL(objectUrl);
	} catch {
		// Fallback: open in a new tab if blob download fails
		try {
			window.open(url, '_blank', 'noopener');
		} catch {
			// Silent fail
		}
	}
}

/**
 * Creates an abort controller with a timeout
 */
export function withTimeout(ms: number) {
	const ctrl = new AbortController();
	const id = setTimeout(() => ctrl.abort('timeout'), ms);
	return { signal: ctrl.signal, clear: () => clearTimeout(id) };
}
