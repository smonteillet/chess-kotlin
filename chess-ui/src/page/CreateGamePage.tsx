import Input from '../ui/Input';
import Button from '../ui/Button';
import {useState} from 'react';
import {GameService} from '../service/GameService';
import {useNavigate} from 'react-router-dom';

const CreateGamePage = () => {

    const [username, setUsername] = useState('');
    const navigate = useNavigate();
    const onClick = async () => {
        const gameId = await new GameService().createGame(username)
        navigate(gameId)
    }


    return <div>
        <Input type="text"  placeholder=" name"  onChange={(event) => setUsername(event.target.value)}
               value={username}/>
        <Button onClick={onClick}>Create Game</Button>
    </div>
}
export default CreateGamePage