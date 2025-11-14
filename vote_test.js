import http from "k6/http";
import { sleep, check } from "k6";

export const options = {
  vus: 1,
  iterations: 1,
};

export default function () {
  const votePayload = JSON.stringify({
    userId: "synthetic-test-001",
    candidateId: "A"
  });

  const params = {
    headers: {
      "Content-Type": "application/json"
    }
  };

  const start = Date.now();

  const voteRes = http.post("http://localhost:8080/vote", votePayload, params);
  check(voteRes, { "vote inserted": r => r.status === 200 });

  sleep(0.2);

  const lbRes = http.get("http://localhost:8080/leaderboard");
  check(lbRes, { "leaderboard ok": r => r.status === 200 });

  const totalMs = Date.now() - start;
  console.log(`Realtime delta: ${totalMs} ms`);

  check(null, { "Realtime ≤ 500 ms": () => totalMs <= 500 });
}