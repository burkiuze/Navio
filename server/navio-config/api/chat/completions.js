// OpenAI-compatible chat endpoint for Navio. Signed-in users only (Clerk session JWT); the
// OpenRouter key stays in this project's env vars.
import { verifyClerkToken, json } from '../_auth.js';

export const config = { runtime: 'edge' };

export default async function handler(req) {
  if (req.method !== 'POST') return json(405, { error: { message: 'POST only', code: 405 } });
  const claims = await verifyClerkToken(req);
  if (!claims) return json(401, { error: { message: 'Oturum geçersiz, tekrar giriş yapın.', code: 401 } });
  const key = process.env.OPENROUTER_API_KEY;
  if (!key) return json(500, { error: { message: 'OPENROUTER_API_KEY is not configured', code: 500 } });

  const body = await req.json().catch(() => null);
  if (!body || !Array.isArray(body.messages)) return json(400, { error: { message: 'messages required', code: 400 } });
  body.user = claims.sub;
  delete body.stream;

  const upstream = await fetch('https://openrouter.ai/api/v1/chat/completions', {
    method: 'POST',
    headers: {
      authorization: `Bearer ${key}`,
      'content-type': 'application/json',
      'http-referer': 'https://github.com/burkiuze/Navio',
      'x-title': 'Navio',
    },
    body: JSON.stringify(body),
  });
  return new Response(upstream.body, {
    status: upstream.status,
    headers: { 'content-type': upstream.headers.get('content-type') || 'application/json' },
  });
}
