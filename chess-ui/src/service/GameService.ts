export class GameService {

    private API_BASE_URL = 'http://localhost:8080'

    async createGame(username: string) {
        const response = await fetch(`${this.API_BASE_URL}/game/create/${username}`, {
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                },
                method: 'POST'
            }
        )
        const data = await response.json()
        return data.value
    }
}