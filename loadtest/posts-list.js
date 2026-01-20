import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const LIMIT = Number(__ENV.LIMIT || "20");
const OFFSET = Number(__ENV.OFFSET || "0");
const PAGE = Math.floor(OFFSET / LIMIT) + 1;

export const options = {
    vus: Number(__ENV.VUS || "1"),
    duration: __ENV.DURATION || "20s",
    thresholds: {
        http_req_failed: ["rate<0.01"],
    },
};

export default function () {
    const url = `${BASE_URL}/api/v1/posts?page=${PAGE}&size=${LIMIT}`;
    const res = http.get(url, { tags: { name: `page_${PAGE}` } });

    check(res, {
        "status is 200": (r) => r.status === 200,
    });

    sleep(0.2);
}
