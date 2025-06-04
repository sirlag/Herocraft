import { PUBLIC_API_BASE_URL } from '$env/static/public';
import { json } from '@sveltejs/kit';

export async function POST({request, cookies, params}) {
	console.log(request)

	const auth = cookies.get('user_session');

	if (auth === undefined) {
		return json ({"error": "Invalid request"}, {status: 400});
	}

	let favoriteResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${params.slug}/favorite`, {
		method: 'POST',
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json',
			Cookie: request.headers.get('Cookie')!!,
			'accept': 'application/json'
		}
	})


	return favoriteResponse;

}

export async function DELETE({request, cookies, params}) {
	console.log(request)

	const auth = cookies.get('user_session');

	if (auth === undefined) {
		return json ({"error": "Invalid request"}, {status: 400});
	}

	let favoriteResponse = await fetch(`${PUBLIC_API_BASE_URL}/decks/${params.slug}/favorite`, {
		method: 'DELETE',
		credentials: 'include',
		headers: {
			'Content-Type': 'application/json',
			Cookie: request.headers.get('Cookie')!!,
			'accept': 'application/json'
		}
	})


	return favoriteResponse;
}

