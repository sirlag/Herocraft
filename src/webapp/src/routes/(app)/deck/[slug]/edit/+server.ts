import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { json } from '@sveltejs/kit';

export async function POST({request, cookies, params}) {
	console.log(request)

	const { cardId, count } = await request.json();
	const auth = cookies.get('user_session');
	console.log(cardId, count)
	console.log(cookies.get('user_session'))

	if (cardId === undefined || count === undefined || auth === undefined) {
		return json ({"error": "Invalid request"}, {status: 400});
	}

	let modifyResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${params.slug}/edit`, {
		method: 'POST',
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json',
			Cookie: request.headers.get('Cookie')!!,
			'accept': 'application/json'
		},
		body: JSON.stringify({ cardId, count })
	})

	console.log(modifyResponse)

	return modifyResponse;

}